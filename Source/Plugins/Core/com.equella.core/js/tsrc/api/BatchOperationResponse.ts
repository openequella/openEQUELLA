export interface BatchOperationResponse {
  id: string;
  status: number;
  message: string;
}

/**
 * Group responses that have non 2xx codes and return their messages
 */
export function groupErrorMessages(
  responses: BatchOperationResponse[]
): string[] {
  return responses
    .filter(response => response.status >= 400)
    .map(response => response.message);
}
