<template>
    <v-container>
        <v-form class="bvm-ledger-search"
            @submit.prevent="performSearch">
            <v-text-field
                v-model="searchTerm"
                class="pr-5"
                label="Any text"
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
          <v-select
              label="State"
              class="pr-5"
              variant="outlined"
              v-model="state"
              tile
              flat
              :items="['ANY', 'OPEN', 'CLOSED', 'IN_RECOVERY']"
              @change="performSearch"
          />
            <v-text-field
                v-model="minLength"
                class="pr-5"
                label="Min size"
                suffix="MB"
                tile
                type="number"
                flat
                hide-details
                @keydown.enter="performSearch" />
            <v-text-field
                v-model="maxLength"
                class="pr-5"
                label="Max size"
                type="number"
                suffix="MB"
                tile
                flat
                hide-details
                @keydown.enter="performSearch" />
             <v-text-field
                v-model="minAge"
                class="pr-5"
                label="Older than"
                tile
                type="number"
                flat
                suffix="Minutes"
                hide-details
                @keydown.enter="performSearch" />
            <v-btn
                depressed
                large
                tile
                class="mt-1"
                color="primary white--text"
                @click="performSearch">
                Search
            </v-btn>
            <v-btn
                depressed
                large
                tile
                class="ml-1 mt-1"
                color="red lighten-1 white--text"
                @click="clearSearch">
                Clear
            </v-btn>
        </v-form>
        <v-alert
            border="left"
            color="primary white--text">
            Found: <b>{{ ledgersCount }}</b> ledgers,
            total size: <b>{{ $library.formatBytes(totalSize) }}</b>.
        </v-alert>
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
                    :bookies="bookies"
                    :bookies-topology="bookiesTopology"
                />
            </div>
        </div>
        <v-row v-if="ledgers.length > 0">
            <v-col cols="4" justify="start">
                <v-select
                    v-model="size"
                    :items="[20, 40, 80, 160]"
                    label="Show ledgers"
                    color="primary"
                    class="my-1"
                    outlined
                    dense
                />
            </v-col>
            <v-col cols="8" justify="end">
                <v-pagination
                    v-show="pageLength > 1"
                    v-model="page"
                    :length="pageLength"
                    color="primary"
                    class="justify-end"
                />
            </v-col>
        </v-row>
    </v-container>
</template>
<script>
const DefaultPageSize = 20;
import qs from 'query-string';
import MetadataContainer from "@/components/MetadataContainer";
import Ledger from "@/components/Ledger";
export default {
    components: {
        MetadataContainer,
        Ledger,
    },
    data() {
        return {
            page: 1,
            size: DefaultPageSize,
            search: false,
            searchTerm: '',
            ledgerIds: '',
            state: 'ANY',
            minLength: '',
            maxLength: '',
            minAge: '',
            showLedgerMetadata: false,
            currentLedger: null,
            ledgers: [],
            ledgersCount: 0,
            totalSize: 0,
            bookiesTopology: {},
            bookies: []
        };
    },
    computed: {
        pageLength() {
            return Math.ceil(this.ledgersCount / this.size);
        }
    },
    watch: {
        async page(newPageValue) {
            return this.refreshLedgers(newPageValue, this.size);
        },
        async size(newSizeValue) {
            this.page = 1;
            return this.refreshLedgers(this.page, newSizeValue);
        }
    },
    async created() {
        return this.refreshLedgers(1, DefaultPageSize);
    },
    methods: {
        async refreshLedgers(page, size) {
            const params = { page, size };

            if (this.search) {
                params.term = this.searchTerm;
                params.ledgerIds = this.ledgerIds;
                if (this.state !== "ANY") {
                    params.state = this.state;
                }
                params.minLength = this.minLength > 0 ? Math.ceil(this.minLength * 1024 * 1024) : '';
                params.maxLength = this.maxLength > 0 ? Math.ceil(this.maxLength * 1024 * 1024) : '';
                params.minAge = !this.minAge ? 0 : this.minAge;
            }
            if (this.$route.meta.type === "bookie") {
                const { bookieId, clusterId } = this.$route.params;
                params.bookieId = bookieId;
                params.clusterId = clusterId;
            }

            const queryParameters = qs.stringify(params);
            let url = `api/ledger/all?${queryParameters}`;

            const ledgersResult = await this.$request.get(url);
            this.ledgers = ledgersResult.ledgers;
            this.ledgersCount = ledgersResult.totalLedgers;
            this.totalSize = ledgersResult.totalSize;


            this.$request.get(`api/topology/all`).then((result) => this.bookiesTopology = result.bookies)
            this.$request.get('api/bookie/all?' + qs.stringify({page: 1, size: 1000})).then((result) => this.bookies = result.bookies)

        },
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
        async performSearch() {
            this.search = true;
            this.closeMetadata();
            return this.refreshLedgers(1, this.size);
        },
        async clearSearch() {
            this.searchTerm = '';
            this.ledgerIds = '';
            this.state = "ANY";
            this.minLength = '';
            this.maxLength = '';
            this.minAge = '';

            this.search = false;
            this.closeMetadata();
            return this.refreshLedgers(1, this.size);
        }
    }
}
</script>
