export const baseApi = (uri: string, method: string, body?: BodyInit) => {
  const url: string = `http://${
    import.meta.env.VITE_BACKEND_SERVER
  }/api/${uri}`;

  const options: RequestInit = {
    method,
    headers: {
      "Content-Type": "application/json",
      Origin: `http://${import.meta.env.VITE_FRONTEND_SERVER}`,
    },
    credentials: "include",
  };

  if (body) {
    options.body = body;
  }

  return fetch(url, options).then((response) => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  });
};
