<template>
    <div>
        <v-form class="bvm-ledger-search"
            @submit.prevent="performSearch" >
            <v-text-field
                v-model="searchTerm"
                class="pr-5"
                label="Search"
                tile
                flat
                hide-details>
            </v-text-field>
            <v-text-field
                v-model="ledgerIds"
                class="pr-5"
                label="Ledger Id"
                tile
                flat
                hide-details>
            </v-text-field>
            <v-text-field
                v-model="minLength"
                class="pr-5"
                label="Min Length"
                tile
                flat
                hide-details>
            </v-text-field>
            <v-text-field
                v-model="maxLength"
                class="pr-5"
                label="Max Length"
                tile
                flat
                hide-details>
            </v-text-field>
             <v-text-field
                v-model="minAge"
                class="pr-5"
                label="Age"
                tile
                flat
                hide-details>
            </v-text-field>
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
        <div class="bvm-ledger" :class="{'metadata': showLedgerMetadata}">
            <div class="bvm-tile-container">
                <Tile
                    v-for="item in ledgers"
                    :item="item"
                    :key="item.id"
                    @click="showMetadata(item.id)"
                />
            </div>
            <div v-if="showLedgerMetadata"
                class="bvm-metadata-container">
                <MetadataContainer
                    :currentLedger="currentLedger"
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
            ledgers: []
        };
    },
    methods: {
        showMetadata(ledgerId) {
            this.$request.get(`api/ledger/metadata/${ledgerId}`).then(
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
            let url = "api/ledger/all?term="+encodeURIComponent(this.searchTerm)
                        +"&ledgerIds="+encodeURIComponent(this.ledgerIds)
                        +"&minLength="+encodeURIComponent(this.minLength)
                        +"&maxLength="+encodeURIComponent(this.maxLength)
                        +"&minAge="+encodeURIComponent(this.minAge);
            if (this.$route.meta.type === "bookie") {
                const bookieId = this.$route.params.bookieId;
                url = url + "&bookie="+encodeURIComponent(bookieId);
            }
            this.$request.get(url).then(
                ledgers => {
                    this.ledgers = ledgers;
                }
            );
        }
    },
    created() {
        let url = "api/ledger/all";
        if (this.$route.meta.type === "bookie") {
            const bookieId = this.$route.params.bookieId;
            url = "api/ledger/all?bookie="+encodeURIComponent(bookieId);
        }
        this.$request.get(url).then(
            ledgers => {
                this.ledgers = ledgers;
            }
        );
    }
}
</script>
