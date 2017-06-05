/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.packman.bomtool.go

import com.google.gson.annotations.SerializedName

class GodepsFile {
    @SerializedName("ImportPath")
    String importPath
    @SerializedName("GoVersion")
    String goVersion
    @SerializedName("GodepVersion")
    String godepVersion
    @SerializedName("Packages")
    String[] packages
    @SerializedName("Deps")
    GoDependency[] deps
}
