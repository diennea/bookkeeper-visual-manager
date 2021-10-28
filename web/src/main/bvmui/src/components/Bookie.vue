<template>
    <v-card class="d-inline-block mx-2 mb-10" width="400" max-width="400" :color="statusColor" raised dark @click="$emit('click', $event)">
        <v-list-item two-line>
        <v-list-item-content>
            <v-list-item-title class="headline">{{ bookie.bookieId }} {{ bookie.clusterName }}</v-list-item-title>
            <v-list-item-subtitle>{{ $library.formatDate(bookie.lastScan) }}</v-list-item-subtitle>
        </v-list-item-content>
        </v-list-item>
        <v-list-item>
            <v-list-item-title>Usage</v-list-item-title>
            <v-list-item-subtitle class="text-right">{{ $library.formatPercent(bookie.totalDiskSpace - bookie.freeDiskSpace, bookie.totalDiskSpace) }}&#37;</v-list-item-subtitle>
        </v-list-item>
        <v-card-text>
            <p class="text-center text-uppercase display-1">{{ bookie.state }}</p>
        </v-card-text>
        <v-divider />
        <v-card-actions>
            <v-btn text @click.stop="$emit('click-info', $event)" :disabled="!enableShowInfo">
                Show info
            </v-btn>
            <v-btn text @click.stop="$emit('click-gc', $event)" :disabled="!enableShowInfo || !httpServerIsEnabled">
                Garbage Collector
            </v-btn>
        </v-card-actions>
    </v-card>
</template>
<script>
export default {
    props: {
        bookie: Object
    },
    computed: {
        statusColor() {
            switch (this.bookie.state) {
                case 'available':
                    return 'light-blue darken-1';
                case 'readonly':
                    return 'blue-grey darken-1';
                case 'down':
                default:
                    return 'blue-grey darken-4';
            }
        },
        enableShowInfo() {
            const state = this.bookie.state ? this.bookie.state : "";
            switch (state) {
                case 'available':
                case 'readonly':
                    return true;
                case 'down':
                default:
                    return false;
            }
        },
        httpServerIsEnabled() {
            const bookieHttpServerUri = this.bookie.endpoints["httpserver"];
            if(bookieHttpServerUri !== undefined) {
                return true;
            } else {
                return false;
            }
        }
    }
};
</script>
