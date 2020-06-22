/*
 * Licensed to Diennea S.r.l. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Diennea S.r.l. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.bkvm.cache;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author eolivelli
 */
@Entity(name = "cluster")
public class Cluster implements Serializable {

    @Id
    @Column(columnDefinition = "string")
    private String name;

    @Column(columnDefinition = "string")
    private String metadataServiceUri;

    public Cluster() {
    }

    public Cluster(int clusterId, String name, String metadataServiceUri) {
        this.name = name;
        this.metadataServiceUri = metadataServiceUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetadataServiceUri() {
        return metadataServiceUri;
    }

    public void setMetadataServiceUri(String metadataServiceUri) {
        this.metadataServiceUri = metadataServiceUri;
    }

}
