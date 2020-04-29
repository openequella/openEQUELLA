export interface BatchOperationResponse {
  id: string;
  status: number;
  message: string;
}

/**
 * Group responses that have a 4xx or 5xx status code, and return their messages
 */
export function groupErrorMessages(
  responses: BatchOperationResponse[]
): string[] {
  return responses
    .filter((response) => response.status >= 400)
    .map((response) => response.message);
}
