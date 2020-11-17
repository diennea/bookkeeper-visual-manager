<template>
    <div class="bvm-bookie">
        <Card
            v-for="(item, index) in bookies"
            :data="item"
            :key="index"
            @click="openBookie(item)"
        />
    </div>
</template>
<script>
import Card from "@/components/Card";
export default {
    components: {
        Card
    },
    data() {
        return {
            bookies: []
        };
    },
    created() {
        this.$request.get("api/bookie/all")
            .then(bookies => {
                this.bookies = bookies;
            });
    },
    methods: {
        openBookie(item) {
            this.$router.push({
                name: "bookie-ledgers",
                params: { bookieId: item.description }
            });
        }
    }
};
</script>
