<template>
    <div class="bvm-tile" @click="$emit('click', $event)">
        <div v-if="item.description !== ''" class="bvm-tile__descrow">
            <span>{{ item.description }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Ledger {{ item.id }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Size {{ $library.formatBytes(item.length) }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Replication {{ item.writeQuorumSize }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Age {{ item.age }} hours</span>
        </div>
        <div class="bvm-tile__descrow">
            <span>Cluster {{ item.clusterName }}</span>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        item: Object
    }
};
</script>
