<template>
    <div class="bvm-bookie">
        <CardContainer v-if="pageLoaded" :items="bookies" @item-clicked="itemClicked"/>
        <Spinner v-else/>
    </div>
</template>
<script>
export default {
    data: function() {
        return {
            pageLoaded: false,
            bookies: []
        };
    },
    methods: {
        redirectToPage(page, params) {
            const _params = !params ? {} : params;
        },
        itemClicked(item) {
            this.$router.push({
                name: "bookie-ledgers",
                params: { bookieId: item.description }
            });
        }
    },
    created: function() {
        this.$request.get(
            "api/bookie/all",
            bookies => {
                this.pageLoaded = true;
                this.bookies = bookies;
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

