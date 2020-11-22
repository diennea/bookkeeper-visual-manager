<template>
    <v-container class="bvm-bookie">
        <v-row justify="start">
            <Bookie
                v-for="bookie in bookies"
                :bookie="bookie"
                :key="keyBookie(bookie)"
                @click="openBookie(bookie)"
            />
        </v-row>
        <v-row v-if="bookies.length > 0">
            <v-col cols="4">
                <v-select
                    :value="size"
                    @input="refreshBookies(page, $event)"
                    :items="[4, 8, 16, 32]"
                    label="Show bookies"
                    color="blue lighten-1"
                    outlined
                    dense
                />
            </v-col>
            <v-col cols="8" justify="end">
                <v-pagination
                    v-show="pageLength > 1"
                    :value="page"
                    @input="refreshBookies($event, size)"
                    :length="pageLength"
                    color="blue lighten-1"
                    class="justify-end my-1"
                />
            </v-col>
        </v-row>
    </v-container>
</template>
<script>
const DefaultPageSize = 8;
import Bookie from "@/components/Bookie";
export default {
    components: {
        Bookie
    },
    data() {
        return {
            page: 0,
            size: 0,
            bookies: [],
            bookiesCount: 0,
        };
    },
    computed: {
        pageLength() {
            return Math.ceil(this.bookiesCount / this.size);
        }
    },
    async created() {
        return this.refreshBookies(1, DefaultPageSize);
    },
    methods: {
        async refreshBookies(page, size) {
            console.log("Ciao", size, page)
            if (this.page == page && this.size == size) return;
            this.page = this.size === size ? page : 1;
            this.size = size;
            const url = `api/bookie/all?page=${this.page}&size=${this.size}`;
            const bookieResponse = await this.$request.get(url);

            this.bookies = bookieResponse.bookies;
            this.bookiesCount = bookieResponse.totalSize;
        },
        openBookie(bookie) {
            const { clusterId, bookieId } = bookie;
            this.$router.push({
                name: "bookie-ledgers",
                params: { clusterId, bookieId }
            });
        },
        keyBookie(bookie) {
            return `${bookie.clusterId}|${bookie.bookieId}`;
        }
    }
};
</script>
