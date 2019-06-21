import * as React from "react";

export function LegacyForm(props: {
  state: { [key: string]: string[] };
  children: React.ReactNode;
}) {
  const { state, children } = props;
  return (
    <form name="eqForm" id="eqpageForm" onSubmit={e => e.preventDefault()}>
      <div style={{ display: "none" }} className="_hiddenstate">
        {Object.keys(state).map((k, i) => {
          return (
            <React.Fragment key={i}>
              {state[k].map((v, i) => (
                <input key={i} type="hidden" name={k} value={v} />
              ))}
            </React.Fragment>
          );
        })}
      </div>
      {children}
    </form>
  );
}
