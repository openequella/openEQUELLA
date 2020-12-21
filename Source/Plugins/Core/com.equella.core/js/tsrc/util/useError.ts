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
import { useEffect, useState } from "react";

/**
 * A custom hook to assist with the management of of state (and effect dependencies) for the
 * displaying of errors within the New UI Template. Ideally long term this will be replaced
 * possibly through the centralising of this via a Context with `useContent`. Further, the
 * project will start to make use of Error Boundaries (possibly via `react-error-boundary`).
 *
 * But for now, we have this...
 *
 * @param onError A function which typically calls out to updateTemplate to trigger the error
 *                mechanisms, but often passed in via props
 */
export const useError = (
  onError: (error: Error) => void
): ((error: Error) => void) => {
  const [error, setError] = useState<Error>();

  useEffect(() => {
    if (error) {
      onError(error);
      setError(undefined);
    }
  }, [error, onError]);

  return setError;
};

export default useError;
