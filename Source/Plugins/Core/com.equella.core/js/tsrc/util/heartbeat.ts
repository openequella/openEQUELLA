import Axios from "axios";

export function startHeartbeat() {
  setInterval(function () {
    Axios.get<string>("api/status/heartbeat").then((resp) => {
      if (resp.data !== "OK") {
        console.log(resp.data);
      }
    });
  }, 2 * 60 * 1000);
}
