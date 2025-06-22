export const getRoute = async (start, end) => {
  const accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

  const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${start[0]},${start[1]};${end[0]},${end[1]}?geometries=geojson&steps=true&access_token=${accessToken}`;

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Mapbox API error: ${response.status}`);
  }

  const data = await response.json();

  const route = data.routes[0];
  const steps = route.legs[0].steps;

  return {
    geometry: route.geometry,
    steps: steps,
  };
};
