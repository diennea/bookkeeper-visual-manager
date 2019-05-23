<template>
    <div class="bvm-ledger">
        <p v-if="pageLoaded">{{ledgers}}</p>
        <Spinner v-else/>
    </div>
</template>
<script>
export default {
    data: function() {
        return {
            pageLoaded: false,
            ledgers: []
        };
    },
    methods: {},
    created: function() {
        this.$request.get(
            "api/ledger/all",
            ledgers => {
                this.pageLoaded = true;
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

