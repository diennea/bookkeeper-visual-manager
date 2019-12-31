<template>
    <div class="bvm-bookie">
        <CardContainer v-if="pageLoaded" :items="bookies" @item-clicked="itemClicked" />
        <Spinner v-else />
    </div>
</template>
<script>
import CardContainer from "@/components/CardContainer";
import Spinner from "@/components/Spinner";
export default {
    components: {
        CardContainer,
        Spinner
    },
    data() {
        return {
            pageLoaded: false,
            bookies: []
        };
    },
    methods: {
        itemClicked(item) {
            this.$router.push({
                name: "bookie-ledgers",
                params: { bookieId: item.description }
            });
        }
    },
    created() {
        this.$request.get("api/bookie/all", bookies => {
            this.pageLoaded = true;
            this.bookies = bookies;
        });
    }
};
</script>

