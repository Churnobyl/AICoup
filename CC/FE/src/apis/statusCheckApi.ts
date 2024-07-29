import { baseApi } from "./baseApi";

export const statusCheckApi = () => {
  return baseApi("status-check", "GET").then((response) => {
    return response;
  });
};
