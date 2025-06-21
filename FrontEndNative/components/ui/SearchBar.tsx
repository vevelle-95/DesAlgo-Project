import React, { useState } from "react";
import { View, TextInput, StyleSheet } from "react-native";
import axios from "axios";

const MAPBOX_TOKEN = "YOUR_MAPBOX_ACCESS_TOKEN";

export default function SearchBar({
  onSelectOrigin,
  onSelectDestination,
}: any) {
  const [originText, setOriginText] = useState("");
  const [destText, setDestText] = useState("");

  const geocode = async (place: string, setCoord: any) => {
    try {
      const response = await axios.get(
        `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
          place
        )}.json`,
        {
          params: {
            access_token: MAPBOX_TOKEN,
            limit: 1,
          },
        }
      );
      const [lng, lat] = response.data.features[0].center;
      setCoord({ latitude: lat, longitude: lng });
    } catch (error) {
      console.error("Geocoding failed:", error);
    }
  };

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        placeholder="Enter Origin Address"
        value={originText}
        onChangeText={(text) => setOriginText(text)}
        onSubmitEditing={() => geocode(originText, onSelectOrigin)}
      />
      <TextInput
        style={styles.input}
        placeholder="Enter Destination Address"
        value={destText}
        onChangeText={(text) => setDestText(text)}
        onSubmitEditing={() => geocode(destText, onSelectDestination)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 10,
    backgroundColor: "#eee",
  },
  input: {
    backgroundColor: "white",
    padding: 8,
    marginBottom: 5,
    borderRadius: 5,
  },
});
