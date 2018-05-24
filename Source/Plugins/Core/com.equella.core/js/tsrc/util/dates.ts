
import * as moment from 'moment'

const IsoFormat = 'YYYY-MM-DD\'T\'HH:mm:ss.SSSZ'

export function parseISO(isoString: string) {
    return moment(isoString, IsoFormat);
}

export function formatISO(dateTime: Date) {
    return moment(dateTime, IsoFormat); 
}