
declare module 'react-visibility-sensor'
{
    import * as React from 'react';

    interface VisibilitySensorProps
    {
        onChange: (visible:boolean) => void;
    }
    class VisibilitySensor extends React.Component<VisibilitySensorProps> {}

    export = VisibilitySensor;
}