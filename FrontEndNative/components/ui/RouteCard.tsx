import React from "react";
import { View, Text, StyleSheet } from "react-native";

export default function RouteCard({ mode, duration, cost, traffic }: any) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>{mode.toUpperCase()} ROUTE</Text>
      <Text>Duration: {duration}</Text>
      <Text>Cost: {cost}</Text>
      <Text>Traffic: {traffic}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    padding: 15,
    backgroundColor: "#f9f9f9",
    borderTopWidth: 1,
    borderColor: "#ddd",
  },
  title: {
    fontWeight: "bold",
    marginBottom: 5,
  },
});
