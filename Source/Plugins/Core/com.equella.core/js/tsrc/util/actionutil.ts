import { Dispatch } from "redux";
import { AsyncActionCreators } from "typescript-fsa";
import actionCreatorFactory from "typescript-fsa";

export const actionCreator = actionCreatorFactory();

/**
 * Handle asynchronous actions in the redux store
 *
 * @param asyncAction redux action to map to
 * @param worker asynchronous callback of work to be done
 *
 * NOTE: originally from https://github.com/aikoven/typescript-fsa/issues/5#issuecomment-255347353
 */
export function wrapAsyncWorker<TParameters, TSuccess, TError>(
  asyncAction: AsyncActionCreators<TParameters, TSuccess, TError>,
  worker: (params: TParameters) => Promise<TSuccess>
) {
  return function wrappedWorker(
    dispatch: Dispatch<any>,
    params: TParameters
  ): Promise<TSuccess> {
    dispatch(asyncAction.started(params));
    return worker(params).then(
      (result) => {
        dispatch(asyncAction.done({ params, result }));
        return result;
      },
      (error: TError) => {
        dispatch(asyncAction.failed({ params, error }));
        throw error;
      }
    );
  };
}
