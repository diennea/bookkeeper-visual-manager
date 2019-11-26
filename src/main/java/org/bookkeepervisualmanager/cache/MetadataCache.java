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
import java.util.stream.Collectors;
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

    public void deleteLedger(long ledgerId) {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ledger_metadata lm where lm.ledgerId=" + ledgerId).executeUpdate();
        em.createQuery("DELETE FROM ledger_bookie lm where lm.ledgerId=" + ledgerId).executeUpdate();
        em.createQuery("DELETE FROM ledger lm where lm.ledgerId=" + ledgerId).executeUpdate();
        em.getTransaction().commit();
    }
    public void updateLedger(Ledger ledger, List<LedgerBookie> bookies,
            List<LedgerMetadataEntry> metadataEntries) {
        em.getTransaction().begin();
        Ledger exists = em.find(Ledger.class, ledger.getLedgerId());
        if (exists != null) {
            em.merge(ledger);
        } else {
            em.persist(ledger);
        }
        em.createQuery("DELETE FROM ledger_metadata lm where lm.ledgerId=" + ledger.getLedgerId()).executeUpdate();
        em.createQuery("DELETE FROM ledger_bookie lm where lm.ledgerId=" + ledger.getLedgerId()).executeUpdate();
        for (LedgerBookie lb : bookies) {
            em.persist(lb);
        }
        for (LedgerMetadataEntry lme : metadataEntries) {
            em.persist(lme);
        }
        em.getTransaction().commit();
    }

    public List<Ledger> listLedgers() {
        Query q = em.createQuery("select l from ledger l", Ledger.class);
        return q.getResultList();
    }

    public List<Ledger> searchLedgers(String metadataTerm, String bookie) {
        if (metadataTerm != null && !metadataTerm.isEmpty() && bookie != null && !bookie.isEmpty()) {
            Query q = em.createQuery("select DISTINCT(l) from ledger l "
                    + "               INNER JOIN ledger_metadata lm on lm.ledgerId = l.ledgerId"
                    + "               INNER JOIN ledger_bookie ln on ln.ledgerId = l.ledgerId "
                    + " where lm.entryValue like :term and ln.bookieAddress = :address", Ledger.class);
            q.setParameter("term", "%" + metadataTerm + "%");
            q.setParameter("address", bookie);
            return q.getResultList();
        } else if (metadataTerm != null && !metadataTerm.isEmpty()) {
            Query q = em.createQuery("select DISTINCT(l) from ledger l INNER JOIN ledger_metadata lm on lm.ledgerId = l.ledgerId"
                    + " where lm.entryValue like :term", Ledger.class);
            q.setParameter("term", "%" + metadataTerm + "%");
            return q.getResultList();
        } else if (bookie != null && !bookie.isEmpty()) {
            Query q = em.createQuery("select DISTINCT(l) from ledger l "
                    + "               INNER JOIN ledger_bookie ln on ln.ledgerId = l.ledgerId "
                    + " where ln.bookieAddress = :address", Ledger.class);
            q.setParameter("address", bookie);
            return q.getResultList();
        } else {
            Query q = em.createQuery("select l from ledger l ", Ledger.class);
            return q.getResultList();
        }
    }

    public Ledger getLedgerMetadata(long id) {
        return em.find(Ledger.class, id);
    }

    @Override
    public void close() {
        em.close();
        emf.close();
    }

    public List<Long> getLedgersForBookie(String bookieId) {
        Query q = em.createQuery("select l from ledger_bookie l where l.bookieAddress = :address", LedgerBookie.class);
        q.setParameter("address", bookieId);
        List<LedgerBookie> mapping = q.getResultList();
        return mapping.stream().map(LedgerBookie::getLedgerId).collect(Collectors.toList());
    }

}
