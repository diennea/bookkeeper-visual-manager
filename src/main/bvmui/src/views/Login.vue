<template>
    <div>
    <div>Search: Keywork: <input v-model="searchTerm">
                 Min Length: <input v-model="minLength" type="number">
                 Max Length:  <input v-model="maxLength" type="number">
                 Age:  <input v-model="minAge" type="number" >
                <button v-on:click="performSearch" >Search</button></div>
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
            username: 'admin',
            password: 'admin'
        };
    },
    methods: {        
       login: function() {
        let url = "api/login";        
        this.$request.get(url,
            res => {
                this.$router.push({
                                    name: "bookies"
                                });
                            }
                    ,
            error => {
                this.$router.push({
                                    name: "bookies"
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

