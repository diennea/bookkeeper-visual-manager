<template>
    <div>
        <v-form class="d-flex mb-5" 
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
        <div class="bvm-ledger">
            <TileContainer 
                v-if="ledgersLoaded" 
                :items="ledgers" 
                @item-clicked="showMetadata" />
            <Spinner v-else/>
            <MetadataContainer
                v-if="showLedgerMetadata"
                :currentLedger="currentLedger"
                @close="closeMetadata"
            />
        </div>
    </div>
</template>
<script>
export default {
    data() {
        return {
            searchTerm: '',
            minLength: '',
            maxLength: '',
            minAge: 0,
            showLedgerMetadata: false,
            currentLedger: null,
            ledgersLoaded: false,
            ledgers: []
        };
    },
    methods: {
        showMetadata(ledgerId) {
            const url = `api/ledger/metadata/${ledgerId}`;
            this.$request.get(
                url,
                ledger => {
                    this.currentLedger = ledger;
                    this.showLedgerMetadata = true;
                },
                error => {
                    this.$router.push({
                        name: "error"
                    });
                }
            );
        },
        closeMetadata() {
            this.metadata = null;
            this.showLedgerMetadata = false;
        },
        performSearch: function() {
            let url = "api/ledger/all?term="+encodeURIComponent(this.searchTerm)
                        +"&minLength="+encodeURIComponent(this.minLength)
                        +"&maxLength="+encodeURIComponent(this.maxLength)
                        +"&minAge="+encodeURIComponent(this.minAge);
            if (this.$route.meta.type === "bookie") {
                const bookieId = this.$route.params.bookieId;
                url = url + "&bookie="+encodeURIComponent(bookieId);
            }
            this.$request.get(url,
                ledgers => {
                    this.ledgersLoaded = true;
                    this.ledgers = ledgers;
                },
                error => {
                    this.$router.push({ name: "error" });
            });
        }
    },
    created() {
        let url = "api/ledger/all";
        if (this.$route.meta.type === "bookie") {
            const bookieId = this.$route.params.bookieId;
            url = "api/ledger/all?bookie="+encodeURIComponent(bookieId);
        }
        this.$request.get(url,
            ledgers => {
                this.ledgersLoaded = true;
                this.ledgers = ledgers;
            },
            error => {
                this.$router.push({
                    name: "error"
                });
        });
    }
}
</script>
