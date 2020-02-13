/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.troubleshooting.integration.actions;

import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.models.*;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PendingPvcAction extends ActionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PendingPvcAction.class);

    private static final String PROVISIONING_FAILED = "ProvisioningFailed";
    private static final String STORAGE_CLASS_NOTFOUND = "storageclass[\\w\\.\\s\\\"]*not found";
    private static final Pattern pattern = Pattern.compile(STORAGE_CLASS_NOTFOUND);

    @Override
    public void process(TroubleshootingContext context) {
        DiagnosisReport report = new DiagnosisReport();
        List<TroubleshootingContext.ResourceInfo> resourceData = context.getResourceInfos().get(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
        //TODO proper error handling
        if (resourceData == null || resourceData.isEmpty()) {
            logger.debug("Cannot find any volumes that have been provisioned");
            return;
        }

        Object obj = context.getAttribute(FailedResourceKey.FAILED_POD);
        if (obj == null) {
            logger.debug("Cannot find any failed pod for to {}", describe());
            return;
        }

        V1Pod pod = (V1Pod) FailedResourceKey.FAILED_POD.getKlazz().cast(obj);

        // Get all the pvc names associated to the failed pod
        List<String> podPvcList = pod.getSpec().getVolumes().stream().map(each -> {
            return each.getPersistentVolumeClaim() != null && each.getPersistentVolumeClaim().getClaimName() != null ? each.getPersistentVolumeClaim().getClaimName() : null;
        }).collect(Collectors.toList());

        // get all the events of pvc's associated with the failed pod
        List<V1Event> eventList = new ArrayList<>();
        resourceData.stream().filter(each -> {
            return each != null && each.getResource() != null && each.getResource() instanceof V1PersistentVolumeClaim;
        }).forEach(resourceInfo -> {
            // match the pvc name from the failed pod in the list of all pvc's
            V1PersistentVolumeClaim persistentVolumeClaim = (V1PersistentVolumeClaim) resourceInfo.getResource();
            if (podPvcList.contains(persistentVolumeClaim.getMetadata().getName())) {
                eventList.addAll(resourceInfo.getEvents());
            }
        });

        if (eventList == null || eventList.isEmpty()) {
            report.setReason(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getMessage());
            context.addReport(report);
            return;
        }

        // ProvisioningFailed
        AtomicReference<String> volume = null;
        boolean provsioningFailed = eventList.stream().anyMatch(each -> {
            volume.set(each.getMetadata().getName());
            return PROVISIONING_FAILED.equals(each.getReason()) && pattern.matcher(each.getMessage()).find();
        });

        List<TroubleshootingContext.ResourceInfo> storageClassResources = context.getResourceInfos().get(ResourceKind.STORAGE_CLASS.getKind());
        if ((storageClassResources == null || storageClassResources.isEmpty()) && provsioningFailed) {
            report.setReason(AbstractedErrorMessage.NO_STORAGE_CLASS_FOUND.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.NO_STORAGE_CLASS_FOUND.getMessage());
            return;
        }
        if (provsioningFailed) {
            report.setReason(AbstractedErrorMessage.INVALID_STORAGE_CLASS.formatReason(context.getServiceInfo().getServiceName()));
            report.setRecommendedFix(AbstractedErrorMessage.INVALID_STORAGE_CLASS.formatMessage(storageClassResources.stream().filter(each -> {
                        return each != null && each.getResource() != null && each.getResource() instanceof V1StorageClass;
                    }).map(each -> {
                        V1StorageClass storageClass = (V1StorageClass) each.getResource();
                        return storageClass.getMetadata().getName();
                    }).collect(Collectors.joining(","))
            ));
        }
    }


    @Override
    public String describe() {
        return "Checks if the pod failure is due to any pending volume";
    }

}