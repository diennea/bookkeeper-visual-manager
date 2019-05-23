<template>
    <div class="bvm-metadata-container">
        <button class="bvm-metadata-close" @click="$emit('close', $event)">✕</button>
        <h2>Metadata: Ledger {{id}}</h2>
        <div class="bvm-metadata-row" v-for="(key, value) in  metadata" :key="key">
            <strong>{{key}}</strong>: {{value}}
        </div>
    </div>
</template>
<script>
export default {
    props: {
        id: Number,
        metadata: Object
    },
    computed: {
        computedMetadata() {
            let result = [];
            for (let metadataValue in this.metadata) {
                
            }
        }
    }
};
</script>

