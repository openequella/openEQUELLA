/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as OEQ from '../src';
import {
  GenericIdentityProvider,
  getIdentityProvider,
  updateIdentityProvider,
} from '../src/Oidc';
import * as TC from './TestConfig';

const auth0: GenericIdentityProvider = {
  name: 'Auth0',
  platform: 'GENERIC',
  authCodeClientId: 'C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ',
  authCodeClientSecret:
    '_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L',
  authUrl: 'https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize',
  keysetUrl: 'https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json',
  tokenUrl: 'https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token',
  defaultRoles: new Set(['admin']),
  enabled: true,
  apiUrl: 'https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users',
  apiClientId: '1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps',
  apiClientSecret:
    'JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I',
};

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe('Identity Provider', () => {
  it('updates the Identity Provider configuration', async () => {
    await expect(
      updateIdentityProvider(TC.API_PATH, auth0)
    ).resolves.not.toThrow();
  });

  it('retrieves the Identity Provider configuration', async () => {
    const idp = await getIdentityProvider(TC.API_PATH);
    expect(idp.platform).toBe('GENERIC');
  });
});
