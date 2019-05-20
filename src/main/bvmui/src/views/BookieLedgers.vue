<template>
    <div class="bvm-ledgers">Bookie Addreess: {{bookieId}}</div>
</template>
<script>
export default {
    data: function() {
        return {
            pageLoaded: false,
            bookieId: this.$route.params.bookieId
        };
    },
    methods: {},
    created: function() {
        const url = `api/ledger/${this.bookieId}`;
        this.$request.get(
            url,
            ledgersId => {
                this.pageLoaded = true;
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

