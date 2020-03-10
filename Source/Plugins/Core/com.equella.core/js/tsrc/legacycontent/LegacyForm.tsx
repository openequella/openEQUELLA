import * as React from "react";
import { ReactNode, Fragment } from "react";

interface LegacyFormProps {
  state: { [key: string]: string[] };
  children: ReactNode;
}

export function LegacyForm({ children, state }: LegacyFormProps) {
  return (
    <form name="eqForm" id="eqpageForm" onSubmit={e => e.preventDefault()}>
      <div style={{ display: "none" }} className="_hiddenstate">
        {Object.keys(state).map((k, i) => {
          return (
            <Fragment key={i}>
              {state[k].map((v, i) => (
                <input key={i} type="hidden" name={k} value={v} />
              ))}
            </Fragment>
          );
        })}
      </div>
      {children}
    </form>
  );
}
