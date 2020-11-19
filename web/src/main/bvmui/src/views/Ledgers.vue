<template>
    <div>
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
                <Tile
                    v-for="item in ledgers"
                    :item="item"
                    :key="item.id+'_'+item.clusterId"
                    @click="showMetadata(item.clusterId, item.id)"
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
    </div>
</template>
<script>
import MetadataContainer from "@/components/MetadataContainer";
import Tile from "@/components/Tile";
export default {
    components: {
        MetadataContainer,
        Tile,
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
