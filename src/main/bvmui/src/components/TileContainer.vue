<template>
    <div class="bvm-tile-container">
        <Tile
            @click="$emit('item-clicked', item)"
            v-for="item in items"
            :id="item"
            :key="item"
        />
    </div>
</template>
<script>
export default {
    props: {
        items: Array
    }
};
</script>

