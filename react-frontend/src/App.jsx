import React, { useState } from "react";
import { Map, NavigationControl } from "react-map-gl";
import MapboxDirections from "@mapbox/mapbox-gl-directions/dist/mapbox-gl-directions";
import "@mapbox/mapbox-gl-directions/dist/mapbox-gl-directions.css";
import "mapbox-gl/dist/mapbox-gl.css";

export default function App() {
  const [viewState, setViewState] = useState({
    longitude: 120.9842,
    latitude: 14.5995,
    zoom: 13,
  });

  return (
    <div
      style={{
        height: "100vh",
        width: "100vw",
        overflow: "hidden",
      }}
    >
      <Map
        {...viewState}
        onMove={(evt) => setViewState(evt.viewState)}
        mapboxAccessToken={import.meta.env.VITE_MAPBOX_TOKEN}
        mapStyle="mapbox://styles/mapbox/streets-v12"
        onLoad={(e) => {
          const map = e.target;
          const directions = new MapboxDirections({
            accessToken: import.meta.env.VITE_MAPBOX_TOKEN,
            unit: "metric",
            profile: "mapbox/driving",
            interactive: true,
          });
          map.addControl(directions, "top-left");
        }}
        style={{ width: "100%", height: "100%" }}
      >
        <NavigationControl position="top-right" />
      </Map>
    </div>
  );
}
