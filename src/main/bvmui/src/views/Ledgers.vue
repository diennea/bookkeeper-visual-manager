<template>
    <div>
    <div>Search: <input v-model="searchTerm"> <button v-on:click="performSearch" >Search</button></div>
    <div class="bvm-ledger">            
        <TileContainer 
            v-if="ledgersLoaded" 
            :items="ledgers" 
            @item-clicked="showMetadata"
        />
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
    data: function() {
        return {
            searchTerm: '',
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
        let url = "api/ledger/all?term="+encodeURIComponent(this.searchTerm);
        if (this.$route.meta.type === "bookie") {
            const bookieId = this.$route.params.bookieId;
            url = "api/ledger/all?term="+encodeURIComponent(this.searchTerm)+"&bookie="+encodeURIComponent(bookieId);
        }
        this.$request.get(url,
            ledgers => {
                this.ledgersLoaded = true;
                this.ledgers = ledgers;
                console.log(ledgers);
            },
            error => {
                this.$router.push({
                                    name: "error"
                                });
                            }
                    );
                }
    },
    created: function() {
        let url = "api/ledger/all";
        if (this.$route.meta.type === "bookie") {
            const bookieId = this.$route.params.bookieId;
            url = "api/ledger/all?bookie="+encodeURIComponent(bookieId);
        }
        this.$request.get(url,
            ledgers => {
                this.ledgersLoaded = true;
                this.ledgers = ledgers;
                 console.log(ledgers);
            },
            error => {
                this.$router.push({
                    name: "error"
                });
            }
        );
    }
};
</script>

