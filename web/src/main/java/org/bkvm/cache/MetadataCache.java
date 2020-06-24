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

import java.text.MessageFormat;
import java.util.ArrayList;
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

    private final EntityManagerFactory entityManagerFactory;

    public MetadataCache(DataSource datasource) {
        Map properties = new HashMap();
        properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, datasource);
        entityManagerFactory = Persistence.createEntityManagerFactory("punit", properties);
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }

    @FunctionalInterface
    private interface EntityManagerSupplier {

        Object execute(EntityManager em);
    }

    private static class EntityManagerWrapper implements AutoCloseable {

        EntityManager em;

        EntityManagerWrapper(EntityManager em) {
            this.em = em;
        }

        public Object execute(EntityManagerSupplier supplier) {
            return supplier.execute(em);
        }

        public Object executeWithTransaction(EntityManagerSupplier supplier) {
            em.getTransaction().begin();
            try {
                Object result = supplier.execute(em);
                em.getTransaction().commit();
                return result;
            } catch (Throwable ex) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw ex;
            }
        }

        @Override
        public void close() {
            em.close();
        }
    }

    private EntityManagerWrapper getEntityManager() {
        return new EntityManagerWrapper(entityManagerFactory.createEntityManager());
    }

    public List<Cluster> listClusters() {
        try (EntityManagerWrapper e = getEntityManager()) {
            List<Cluster> result = (List<Cluster>) e.execute(em -> {
                return em.createQuery("SELECT c FROM cluster c", Cluster.class)
                        .getResultList();
            });
            return result;
        }
    }

    public void updateCluster(Cluster cluster) {
        try (EntityManagerWrapper e = getEntityManager()) {
            e.executeWithTransaction(em -> {
                if (cluster.getClusterId() == null) {
                    Integer max = em.createQuery("SELECT MAX(c.clusterId) FROM cluster c", Integer.class)
                            .getSingleResult();
                    
                    int newClusterId = max == null ? 1 : max + 1;
                    cluster.setClusterId(newClusterId);
                }
                
                em.persist(cluster);
                return null;
            });
        }
    }

    public void deleteCluster(int clusterId) {
        try (EntityManagerWrapper e = getEntityManager()) {
            e.executeWithTransaction(em -> {
                Cluster cluster = em.find(Cluster.class, clusterId);
                em.remove(cluster);
                return null;
            });
        }
    }

    public List<Bookie> listBookies() {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            Query q = em.createQuery("select l from bookie l order by l.bookieId", Bookie.class);
            return q.getResultList();
        }
    }

    public void updateBookie(Bookie bookie) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            em.getTransaction().begin();
            em.merge(bookie);
            em.getTransaction().commit();
        }
    }

    public void deleteBookie(String bookieId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            em.getTransaction().begin();
            Query delete = em.createQuery("DELETE FROM bookie lm where lm.bookieId=:bookieId");
            delete.setParameter("bookieId", bookieId);
            delete.executeUpdate();
            em.getTransaction().commit();
        }
    }

    public Bookie getBookie(String bookieId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            return em.find(Bookie.class, bookieId);
        }
    }

    public List<LedgerBookie> getBookieForLedger(long ledgerId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            Query q = em.createQuery("select l from ledger_bookie l where l.ledgerId = :ledgerId", LedgerBookie.class);
            q.setParameter("ledgerId", ledgerId);
            return q.getResultList();
        }
    }


    public List<Ledger> listLedgers() {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            Query q = em.createQuery("select l from ledger l", Ledger.class);
            return q.getResultList();
        }
    }

    public void updateLedger(Ledger ledger, List<LedgerBookie> bookies,
                             List<LedgerMetadataEntry> metadataEntries) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            em.getTransaction().begin();
            long ledgerId = ledger.getLedgerId();
            innerDeleteLedger(ledgerId, em);
            em.persist(ledger);
            bookies.forEach((lb) -> {
                if (ledgerId != lb.getLedgerId()) {
                    throw new IllegalArgumentException(MessageFormat.format("Invalid {0} for {1}", lb.toString(), ledger.toString()));
                }
                em.persist(lb);
            });
            metadataEntries.forEach((lm) -> {
                if (ledgerId != lm.getLedgerId()) {
                    throw new IllegalArgumentException(MessageFormat.format("Invalid {0} for {1}", lm.toString(), ledger.toString()));
                }
                em.persist(lm);
            });
            em.getTransaction().commit();
        }
    }

    public void deleteLedger(long ledgerId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            em.getTransaction().begin();
            innerDeleteLedger(ledgerId, em);
            em.getTransaction().commit();
        }
    }

    private void innerDeleteLedger(long ledgerId, EntityManager em) {
        em.createQuery("DELETE FROM ledger_metadata lm where lm.ledgerId=" + ledgerId).executeUpdate();
        em.createQuery("DELETE FROM ledger_bookie lm where lm.ledgerId=" + ledgerId).executeUpdate();
        em.createQuery("DELETE FROM ledger lm where lm.ledgerId=" + ledgerId).executeUpdate();
    }

    public Ledger getLedgerMetadata(long ledgerId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            return em.find(Ledger.class, ledgerId);
        }
    }

    public List<Long> getLedgersForBookie(String bookieId) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            Query q = em.createQuery("select l from ledger_bookie l where l.bookieId = :bookieId", LedgerBookie.class);
            q.setParameter("bookieId", bookieId);
            List<LedgerBookie> mapping = q.getResultList();
            return mapping.stream().map(LedgerBookie::getLedgerId).collect(Collectors.toList());
        }
    }


    public List<Ledger> searchLedgers(String metadataTerm, String bookie, List<Long> ledgerIds) {
        try (EntityManagerWrapper emw = getEntityManager()) {
            EntityManager em = emw.em;
            if (ledgerIds == null) {
                if (metadataTerm != null && !metadataTerm.isEmpty() && bookie != null && !bookie.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_metadata lm ON lm.ledgerId = l.ledgerId"
                            + "               INNER JOIN ledger_bookie ln ON ln.ledgerId = l.ledgerId "
                            + " WHERE lm.entryValue LIKE :term"
                            + " AND ln.bookieId = :bookieId"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("term", "%" + metadataTerm + "%");
                    q.setParameter("bookieId", bookie);
                    return q.getResultList();
                } else if (metadataTerm != null && !metadataTerm.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_metadata lm ON lm.ledgerId = l.ledgerId"
                            + " WHERE lm.entryValue LIKE :term"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("term", "%" + metadataTerm + "%");
                    return q.getResultList();
                } else if (bookie != null && !bookie.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_bookie ln ON ln.ledgerId = l.ledgerId "
                            + " WHERE ln.bookieId = :bookieId"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("bookieId", bookie);
                    return q.getResultList();
                } else {
                    Query q = em.createQuery("SELECT l FROM ledger l ", Ledger.class);
                    return q.getResultList();
                }
            } else if (!ledgerIds.isEmpty()) {
                if (metadataTerm != null && !metadataTerm.isEmpty() && bookie != null && !bookie.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_metadata lm ON lm.ledgerId = l.ledgerId"
                            + "               INNER JOIN ledger_bookie ln ON ln.ledgerId = l.ledgerId "
                            + " WHERE l.ledgerId IN :ledgerIds"
                            + " AND lm.entryValue LIKE :term"
                            + " AND ln.bookieId = :bookieId"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("ledgerIds", ledgerIds);
                    q.setParameter("term", "%" + metadataTerm + "%");
                    q.setParameter("bookieId", bookie);
                    return q.getResultList();
                } else if (metadataTerm != null && !metadataTerm.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_metadata lm ON lm.ledgerId = l.ledgerId"
                            + " WHERE l.ledgerId IN :ledgerIds"
                            + " AND lm.entryValue LIKE :term"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("ledgerIds", ledgerIds);
                    q.setParameter("term", "%" + metadataTerm + "%");
                    return q.getResultList();
                } else if (bookie != null && !bookie.isEmpty()) {
                    Query q = em.createQuery("SELECT DISTINCT(l) FROM ledger l "
                            + "               INNER JOIN ledger_bookie ln ON ln.ledgerId = l.ledgerId "
                            + " WHERE l.ledgerId IN :ledgerIds"
                            + " AND ln.bookieId = :bookieId"
                            + " ORDER BY l.ledgerId", Ledger.class);
                    q.setParameter("ledgerIds", ledgerIds);
                    q.setParameter("bookieId", bookie);
                    return q.getResultList();
                } else {
                    Query q = em.createQuery("SELECT l FROM ledger l "
                            + " WHERE l.ledgerId IN :ledgerIds", Ledger.class);
                    q.setParameter("ledgerIds", ledgerIds);
                    return q.getResultList();
                }
            } else {
                return new ArrayList<>();
            }
        }
    }

}
