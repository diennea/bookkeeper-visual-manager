<template>
    <v-container class="bvm-bookie">
        <v-form class="bvm-ledger-search"
            @submit.prevent="performSearch">
            <v-text-field
                v-model="searchTerm"
                class="pr-5"
                label="Search"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
            <v-text-field
                v-model="ledgerIds"
                class="pr-5"
                label="Ledger Id"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
            <v-text-field
                v-model="minLength"
                class="pr-5"
                label="Min Length"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
            <v-text-field
                v-model="maxLength"
                class="pr-5"
                label="Max Length"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
             <v-text-field
                v-model="minAge"
                class="pr-5"
                label="Age"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
            <v-btn
                depressed
                large
                tile
                class="mt-1"
                color="blue lighten-1 white--text"
                @click="performSearch">
                Search
            </v-btn>
        </v-form>
        <div class="bvm-ledger-search-results-info">
            Found: <b>{{ ledgers.length }}</b> ledgers,
            total size: <b>{{ $library.formatBytes(totalSize) }}</b>.
        </div>
        <div class="bvm-ledger" :class="{'metadata': showLedgerMetadata}">
            <div class="bvm-tile-container">
                <Ledger
                    v-for="ledger in ledgers"
                    :ledger="ledger"
                    :key="keyLedger(ledger)"
                    @click="showMetadata(ledger.clusterId, ledger.id)"
                />
            </div>
            <div v-if="showLedgerMetadata"
                class="bvm-metadata-container">
                <MetadataContainer
                    :current-ledger="currentLedger"
                    @close="closeMetadata"
                />
            </div>
        </div>
        <!--
        <v-row v-if="ledger.length > 0">
            <v-col cols="4">
                <v-select
                    :value="size"
                    @input="refreshBookies(page, $event)"
                    :items="[4, 8, 16, 32]"
                    label="Show bookies"
                    color="blue lighten-1"
                    outlined
                    dense
                />
            </v-col>
            <v-col cols="8" justify="end">
                <v-pagination
                    v-show="pageLength > 1"
                    :value="page"
                    @input="refreshBookies($event, size)"
                    :length="pageLength"
                    color="blue lighten-1"
                    class="justify-end my-1"
                />
            </v-col>
        </v-row>
        -->
    </v-container>
</template>
<script>
import MetadataContainer from "@/components/MetadataContainer";
import Ledger from "@/components/Ledger";
export default {
    components: {
        MetadataContainer,
        Ledger,
    },
    data() {
        return {
            searchTerm: '',
            ledgerIds: '',
            minLength: '',
            maxLength: '',
            minAge: 0,
            showLedgerMetadata: false,
            currentLedger: null,
            ledgers: [],
            totalSize: 0
        };
    },
    created() {
        let url = "api/ledger/all";
        if (this.$route.meta.type === "bookie") {
            const { bookieId, clusterId } = this.$route.params;
            url = "api/ledger/all?bookie=" + encodeURIComponent(bookieId)
                + "&cluster=" + encodeURIComponent(clusterId);
        }
        this.$request.get(url).then(
            ledgersResult => {
                this.ledgers = ledgersResult.ledgers;
                this.totalSize = ledgersResult.totalSize;
            }
        );
    },
    methods: {
        keyLedger(ledger) {
            return `${ledger.clusterId}|${ledger.id}`;
        },
        showMetadata(clusterId, ledgerId) {
            this.$request.get(`api/ledger/metadata/${clusterId}/${ledgerId}`).then(
                ledger => {
                    this.currentLedger = ledger;
                    this.showLedgerMetadata = true;
                }
            );
        },
        closeMetadata() {
            this.metadata = null;
            this.showLedgerMetadata = false;
        },
        performSearch() {
            this.closeMetadata();
            let url = "api/ledger/all?term=" + encodeURIComponent(this.searchTerm)
                + "&ledgerIds=" + encodeURIComponent(this.ledgerIds)
                + "&minLength=" + encodeURIComponent(this.minLength)
                + "&maxLength=" + encodeURIComponent(this.maxLength)
                + "&minAge=" + encodeURIComponent(this.minAge);
            if (this.$route.meta.type === "bookie") {
                const bookieId = this.$route.params.bookieId;
                url = url + "&bookie=" + encodeURIComponent(bookieId);
            }
            this.$request.get(url).then(
                ledgersResult => {
                    this.ledgers = ledgersResult.ledgers;
                    this.totalSize = ledgersResult.totalSize;
                }
            );
        }
    }
}
</script>
