<template>
    <div class="bvm-metadata">
        <button class="bvm-metadata-close" @click="$emit('close', $event)">✕</button>
        <h3>Ledger {{ currentLedger.id }}</h3>
        <div v-for="item in mainItems" :key="item.title">{{ item.title }}: <b>{{ item.value }}</b></div>
        <div class="bvm-metadata-row" v-for="(blist, key) in currentLedger.ensembles" :key="key">
            <b>Ensemble {{ key }}</b>: <v-container><v-chip label outlined v-for="item in blist" :key="item">{{ item }}</v-chip></v-container>
            <BookiesTopology :bookies="bookies.filter(b => blist.includes(b.bookieId))" :bookies-topology="bookiesTopology" mini />
        </div>
        <div class="bvm-metadata-row" v-for="(key, value) in currentLedger.metadata" :key="key">
            <strong>{{ value }}</strong>: {{ key }}
        </div>
    </div>
</template>
<script>
import BookiesTopology from "@/components/BookiesTopology";

export default {
    components: {
        BookiesTopology
    },
    props: {
        currentLedger: Object,
        bookiesTopology: Object,
        bookies: Array
    },
    computed: {
        mainItems() {
            const arr = []
            arr.push({ title: 'Description', value: this.currentLedger.description })
            arr.push({ title: 'Cluster', value: this.currentLedger.clusterName })
            arr.push({ title: 'Age', value: this.$library.formatTimeFromMinutes(this.currentLedger.age) })
            arr.push({ title: 'Created at', value: this.$library.formatDate(this.currentLedger.ctime) })
            arr.push({ title: 'State', value: this.currentLedger.state })
            arr.push({ title: 'Size', value: this.$library.formatBytes(this.currentLedger.length) })
            arr.push({ title: 'LastEntryId', value: this.currentLedger.lastEntryId })
            arr.push({ title: 'Password', value: this.currentLedger.password })
            arr.push({ title: 'DigestType', value: this.currentLedger.digestType })
            arr.push({ title: 'MetadataFormatVersion', value: this.currentLedger.metadataFormatVersion })
            arr.push({ title: 'Ensemble size (bookies)', value: this.currentLedger.ensembleSize })
            arr.push({ title: 'Write quorum size (copies)', value: this.currentLedger.ensembleSize })
            arr.push({ title: 'Ack quorum size', value: this.currentLedger.ackQuorumSize })

            arr.filter(item => item.value)
            return arr
        }
    }
};
</script>
