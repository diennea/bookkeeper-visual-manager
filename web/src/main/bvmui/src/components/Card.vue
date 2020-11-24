<template>
    <v-card class="d-inline-block mx-2 mb-10" width="400" max-width="400" :color="statuscolor" raised dark @click="$emit('click', $event)">
        <v-list-item two-line>
        <v-list-item-content>
            <v-list-item-title class="headline">{{ data.bookieId }} {{ data.clusterName }}</v-list-item-title>
            <v-list-item-subtitle>{{ $library.formatDate(data.lastScan) }}</v-list-item-subtitle>
        </v-list-item-content>
        </v-list-item>
        <v-card-text>
            <p class="text-center text-uppercase display-1">{{ data.state }}</p>
        </v-card-text>
        <v-divider />
        <v-list class="transparent">
            <v-list-item>
                <v-list-item-title>Usage</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{ $library.formatPercent(data.totalDiskSpace - data.freeDiskSpace, data.totalDiskSpace) }}&#37;</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
                <v-list-item-title>Endpoints</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{ data.endpoints }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
                <v-list-item-title>Free space</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{ $library.formatBytes(data.freeDiskSpace) }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
                <v-list-item-title>Total space</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{ $library.formatBytes(data.totalDiskSpace) }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item v-if="data.properties !== ''">
                <v-list-item-title>Properties</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{ data.properties }}</v-list-item-subtitle>
            </v-list-item>
        </v-list>
    </v-card>
</template>
<script>
export default {
    props: {
        data: Object
    },
    computed: {
        statuscolor(){
            switch(this.data.state){
                case 'available':
                    return 'light-blue darken-1';
                case 'readonly':
                    return 'blue-grey darken-1';
                case 'down':
                default:
                    return 'blue-grey darken-4';
            }
        }
    }
};
</script>
