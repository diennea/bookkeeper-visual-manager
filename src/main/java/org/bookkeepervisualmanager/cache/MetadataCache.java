/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package org.bookkeepervisualmanager.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.sql.DataSource;
import org.eclipse.persistence.config.PersistenceUnitProperties;

/**
 * Simple cache over metadata stored on BookKeeper Metadata service (ZooKeeper)
 */
public class MetadataCache implements AutoCloseable {

    private final EntityManagerFactory emf;
    private final EntityManager em;

    public MetadataCache(DataSource datasource) {
        Map properties = new HashMap();
        properties
                .put(PersistenceUnitProperties.NON_JTA_DATASOURCE, datasource);
        emf = Persistence.createEntityManagerFactory("punit", properties);
        em = emf.createEntityManager();
    }

    public void updateLedger(Ledger ledger) {
        em.getTransaction().begin();
        Ledger exists = em.find(Ledger.class, ledger.getLedgerId());
        if (exists != null) {
            em.merge(ledger);
        } else {
            em.persist(ledger);
        }
        em.getTransaction().commit();
    }

    public List<Ledger> listLedgers() {
        Query q = em.createQuery("select l from ledger l", Ledger.class);
        return q.getResultList();
    }

    public Ledger getLedgerMetadata(long id) {
        return em.find(Ledger.class, id);
    }

    @Override
    public void close() {
        em.close();
        emf.close();
    }

}
