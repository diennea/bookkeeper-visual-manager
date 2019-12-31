<template>
    <div class="bvm-card-container">
        <Card
            @click="$emit('item-clicked', item)"
            v-for="(item, index) in items"
            :data="item"
            :key="index"
        />
    </div>
</template>
<script>
import Card from "./Card";
export default {
    components: {
        Card
    },
    props: {
        items: Array
    }
};
</script>

