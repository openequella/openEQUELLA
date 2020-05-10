import Axios from 'axios';
import axiosCookieJarSupport from 'axios-cookiejar-support';
import tough from 'tough-cookie'

// So that cookies work when used in non-browser (i.e. Node/Jest) type environments. And seeing
// the oEQ security is based on JSESSIONID cookies currently this is key.
const axios = axiosCookieJarSupport(Axios.create());
axios.defaults.jar = new tough.CookieJar();
axios.defaults.withCredentials = true;

export default axios;