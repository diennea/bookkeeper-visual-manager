<template>
    <div :class="rootClasses" @click="$emit('click', $event)">
        <div v-if="ledger.description !== ''" class="bvm-tile__descrow">
            <span>{{ ledger.description }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Ledger {{ ledger.id }} ({{ ledger.clusterName }})</span>
        </div>
        <div class="bvm-tile__row" v-if="ledger.state !== 'OPEN'">
            <span>Size <b>{{ $library.formatBytes(ledger.length) }}</b></span>
        </div>
        <div class="bvm-tile__row">
            <span>E={{ ledger.ensembleSize }}, W={{ ledger.writeQuorumSize }}, A={{ ledger.ackQuorumSize }}</span>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        ledger: Object
    },
    computed: {
        rootClasses() {
            return ["bvm-tile", this.ledger.state === "CLOSED" ? "closed" : ""];
        },
        computedAge() {
            const countHours = Math.floor(this.ledger.age / 60);
            const countMinutes = this.ledger.age % 60;
            if (countHours >= 1) {
                return `${countHours} hours ${countMinutes} minutes`;
            } else {
                return `${countMinutes} minutes`;
            }
        }
    }
};
</script>
