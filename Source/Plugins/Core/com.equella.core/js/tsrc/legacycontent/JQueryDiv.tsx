import * as React from "react";

export interface JQueryDivProps extends React.HTMLAttributes<HTMLDivElement> {
  html: string;
  script?: string;
  afterHtml?: () => void;
  children?: never;
}

export default React.memo(function JQueryDiv(props: JQueryDivProps) {
  const divElem = React.useRef<HTMLElement>();
  React.useEffect(
    () =>
      function() {
        if (divElem.current) {
          $(divElem.current).empty();
        }
      },
    []
  );
  const withoutOthers = {
    ...props
  };
  delete withoutOthers.afterHtml;
  delete withoutOthers.script;
  delete withoutOthers.html;
  return (
    <div
      {...withoutOthers}
      ref={e => {
        if (e) {
          divElem.current = e;
          $(e).html(props.html);
          if (props.script) (window as any).eval(props.script);
          if (props.afterHtml) props.afterHtml();
        }
      }}
    />
  );
});
