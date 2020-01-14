<template>
    <v-card class="mx-auto" :color="statuscolor" raised dark @click="$emit('click', $event)">
        <v-list-item two-line>
        <v-list-item-content>
            <v-list-item-title class="headline">{{data.description}}</v-list-item-title>
            <v-list-item-subtitle>{{$library.formatDate(data.lastScan)}}</v-list-item-subtitle>
        </v-list-item-content>
        </v-list-item>
        <v-card-text>
            <p class="text-center display-3">{{$library.formatPercent(data.totalDiskSpace - data.freeDiskSpace, data.totalDiskSpace)}}&#37;</p>            
        </v-card-text>
        <v-divider></v-divider>
        <v-list class="transparent">
            <v-list-item>
                <v-list-item-title>Free space</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{$library.formatBytes(data.freeDiskSpace)}}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
                <v-list-item-title>Total space</v-list-item-title>
                <v-list-item-subtitle class="text-right">{{$library.formatBytes(data.totalDiskSpace)}}</v-list-item-subtitle>
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
                case 'down':               
                    return 'blue-grey darken-4';     
                case 'readonly':
                    return 'blue-grey darken-1';     
                default:
            }            
        }  
    }
};
</script>


