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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Bookie information on database
 */
@Data
@EqualsAndHashCode
@Entity(name = "bookie")
@IdClass(BookieKey.class)
public class Bookie implements Serializable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final int STATE_DOWN = 0;
    public static final int STATE_AVAILABLE = 1;
    public static final int STATE_READONLY = 2;

    @Id
    @Column(columnDefinition = "string")
    private String bookieId;

    @Id
    @Column(columnDefinition = "int")
    private int clusterId;

    @Column(columnDefinition = "string")
    private String description;

    @Column(columnDefinition = "int")
    private int state;

    @Column(columnDefinition = "timestamp")
    private java.sql.Timestamp scanTime;

    @Column(columnDefinition = "long")
    private long freeDiskspace;

    @Column(columnDefinition = "long")
    private long totalDiskspace;

    @Column(columnDefinition = "string")
    private String bookieInfo;

    public Bookie() {
    }

    public Bookie(String bookieId, int clusterId, String description, int state, Timestamp scanTime, long freeDiskspace, long totalDiskspace, String bookieInfo) {
        this.bookieId = bookieId;
        this.clusterId = clusterId;
        this.description = description;
        this.state = state;
        this.scanTime = scanTime;
        this.freeDiskspace = freeDiskspace;
        this.totalDiskspace = totalDiskspace;
        this.bookieInfo = bookieInfo;
    }

    @Data
    public static final class BookieInfo {

        private List<EndpointInfo> endpoints = Collections.emptyList();
        private Map<String, String> properties = Collections.emptyMap();

    }

    @Data
    public static final class EndpointInfo {
        private String id = "";
        private String protocol = "";
        private String address = "";
        private String auth = "";
        private String extensions = "";
    }

    public static String formatBookieInfo(BookieInfo info) {
        try {
            return MAPPER.writeValueAsString(info);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    public static BookieInfo parseBookieInfo(String s) {
        if (s == null) {
            return new BookieInfo();
        }
        try {
            return MAPPER.readValue(s, BookieInfo.class);
        } catch (IOException err) {
            return new BookieInfo();
        }
    }

}
