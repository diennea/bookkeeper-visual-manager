<template>
    <v-container>
        <v-card v-for="region in sortedRegions" :key="region" :class="cardClass" color="grey lighten-1" raised dark>
                <v-list-item :class="listClass">
                    <v-list-item-content :class="contentClass">
                        <v-list-item-title :class="titleClass">{{ region }}</v-list-item-title>
                        <v-list-item-subtitle :class="subtitleClass">Region</v-list-item-subtitle>
                    </v-list-item-content>
                </v-list-item>

                <v-card v-for="zone in getSortedKeys(regions[region])" :key="zone" :class="cardClass" color="grey" raised dark>
                    <v-list-item :class="listClass">
                        <v-list-item-content :class="contentClass">
                            <v-list-item-title :class="titleClass">{{ zone }}</v-list-item-title>
                            <v-list-item-subtitle :class="subtitleClass">Zone</v-list-item-subtitle>
                        </v-list-item-content>
                    </v-list-item>
                    <v-card v-for="node in getSortedKeys(regions[region][zone])" :key="node" :class="cardClass" color="green lighten-2" raised dark>
                        <v-list-item :class="listClass">
                        <v-list-item-content :class="contentClass">
                            <v-list-item-title :class="titleClass">{{ node }}</v-list-item-title>
                            <v-list-item-subtitle :class="subtitleClass">Node</v-list-item-subtitle>
                        </v-list-item-content>
                        </v-list-item>

                        <v-container v-if="!mini">
                            <Bookie
                                            v-for="bookie of regions[region][zone][node]"
                                            :bookie="bookie"
                                            :key="bookie.bookieId"
                                            @click="$emit('click', bookie)"
                                            @click-info="$emit('click-info', bookie)"
                                            @click-gc="$emit('click-gc', bookie)"
                            />
                        </v-container>

                        <v-container v-if="mini" justify="center">
                            <v-card v-for="bookie of regions[region][zone][node]" :key="bookie.bookieId" :class="cardClass" :color="'lighten-2 ' + (bookie.state === 'available' ? 'light-blue' : 'grey')" raised dark>
                            <v-list-item :class="listClass">
                            <v-list-item-content :class="contentClass">
                                <v-list-item-title :class="titleClass">{{ bookieShortName(bookie.bookieId) }}</v-list-item-title>
                            </v-list-item-content>
                            </v-list-item>
                            </v-card>
                        </v-container>

                    </v-card>

                </v-card>
            </v-card>

    </v-container>
</template>
<script>
import Bookie from "@/components/Bookie";
export default {
    components: {
        Bookie
    },
    props: {
        bookiesTopology: Object,
        bookies: Array,
        mini: Boolean
    },
    computed: {
        listClass() {
            return this.mini ? "px-1 py-1" : "";
        },
        contentClass() {
            return this.mini ? "mx-2 my-1 px-1 py-1" : "";
        },
        cardClass() {
            return this.mini ? "mx-2 my-1 px-1 py-1" : "d-inline-block px-4 my-4";
        },
        titleClass() {
            return this.mini ? "bvm-topology-card-title-mini" : "bvm-topology-card-title";
        },
        subtitleClass() {
            return this.mini ? "bvm-topology-card-subtitle-mini" : "bvm-topology-card-subtitle";
        },
        regions() {
            if (!this.bookiesTopology) {
                return {};
            }
            const regions = {}
            for (let bookieId in this.bookiesTopology) {
                const bookie = this.bookiesTopology[bookieId]
                const metadata = this.bookies.find(b => b.bookieId === bookieId);
                if (!metadata) {
                    continue;
                }

                if (!regions[bookie.region]) {
                    regions[bookie.region] = {}
                }
                let zones = regions[bookie.region]
                if (!zones[bookie.zone]) {
                    zones[bookie.zone] = {}
                }
                let nodes = regions[bookie.region][bookie.zone]
                if (!nodes[bookie.node]) {
                    nodes[bookie.node] = []
                }
                const bookieNode = nodes[bookie.node]
                bookieNode.push(metadata);
            }
            return regions
        },
        sortedRegions() {
            const keys = Object.keys(this.regions);
            return keys.sort();
        },
    },
    methods: {
        getSortedKeys(from) {
            const keys = Object.keys(from);
            return keys.sort();
        },
        bookieShortName(id) {
            // ipaddress
            if (id.match(/^\d/)) {
                return id;
            }
            return id.split(".")[0];
        },
    }
};
</script>
