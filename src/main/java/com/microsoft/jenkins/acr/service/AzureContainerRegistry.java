/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.jenkins.acr.service;

import com.microsoft.azure.management.containerregistry.Build;
import com.microsoft.azure.management.containerregistry.Build.QueuedQuickBuildDefinitionStages.WithCreate;
import com.microsoft.azure.management.containerregistry.OsType;
import com.microsoft.jenkins.acr.common.QuickBuildRequest;
import com.microsoft.azure.management.containerregistry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AzureContainerRegistry extends AzureService {

    private static AzureContainerRegistry instance;

    private AzureContainerRegistry() {
    }

    public Build queueBuildRequest(String resourceGroupName,
                                   String acrName,
                                   QuickBuildRequest request) {
        WithCreate withCreate = this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .queueQuickBuild()
                .withOSType(OsType.fromString(request.platform()))
                .withSourceLocation(request.sourceLocation())
                .withDockerFilePath(request.dockerFilePath());
        if (request.imageNames() == null || request.imageNames().length == 0) {
            withCreate.withImagePushDisabled();
        } else {
            withCreate.withImagePushEnabled()
                    .withImageNames(request.imageNames());
        }
        return withCreate.create();
    }

    public Collection<String> listResourcesName(String resourceGroupName) {
        List<Registry> registryList = this.getClient()
                .containerRegistries()
                .listByResourceGroup(resourceGroupName);
        Collection<String> registryNameList = new ArrayList<>();
        for (Registry registry : registryList) {
            registryNameList.add(registry.name());
        }
        return registryNameList;
    }

    public String getLog(String resourceGroupName, String acrName, String buildId) {
        return this.getClient()
                .containerRegistries()
                .getByResourceGroup(resourceGroupName, acrName)
                .queuedBuilds()
                .get(buildId)
                .getLogLink();
    }

    public static AzureContainerRegistry getInstance() {
        instance = instance == null ? new AzureContainerRegistry() : instance;
        return instance;
    }
}
