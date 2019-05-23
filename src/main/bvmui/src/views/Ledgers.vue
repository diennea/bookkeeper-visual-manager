<template>
    <div class="bvm-ledger">
        <TileContainer 
            v-if="ledgersLoaded" 
            :items="ledgers" 
            @item-clicked="showMetadata"
        />
        <Spinner v-else/>
        <MetadataContainer
            v-if="showLedgerMetadata"
            :id="currentLedger.id"
            :metadata="currentLedger.metadata"
            @close="closeMetadata"
        />
    </div>
</template>
<script>
export default {
    data: function() {
        return {
            showLedgerMetadata: false,
            currentLedger: null,
            ledgersLoaded: false,
            ledgers: []
        };
    },
    methods: {
        showMetadata(ledger) {
            const url = `api/ledger/metadata/${ledger.id}`;
            this.$request.get(
                url,
                metadata => {
                    this.currentLedger = {
                        id: ledger.id,
                        metadata: metadata
                    }
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
        }
    },
    created: function() {
        let url = "api/ledger/all";
        if (this.$route.meta.type === "bookie") {
            const bookieId = this.$route.params.bookieId;
            url = `api/ledger/bookie/${bookieId}`
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
            }
        );
    }
};
</script>

