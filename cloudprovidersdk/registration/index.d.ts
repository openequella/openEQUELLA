interface OAuthCredentials {
  clientId: string;
  clientSecret: string;
}

interface ServiceUri {
  uri: string;
  authenticated: boolean;
}

export interface ProviderRegistration {
  name: string;
  description?: string;
  vendorId: String;
  baseUrl: string;
  iconUrl?: string;
  providerAuth: OAuthCredentials;
  serviceUris: { [key: string]: ServiceUri };
  viewers: object;
}

interface ProviderRegistrationInstance extends ProviderRegistration {
  oeqAuth: OAuthCredentials;
}

interface ProviderRegistrationResponse {
  instance: ProviderRegistrationInstance;
  forwardUrl: string;
}
