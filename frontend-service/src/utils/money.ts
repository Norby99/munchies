// Money is always a decimal string on the wire — never a float. Format for
// display with Intl.NumberFormat; never do float arithmetic before display.

const formatter = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' })

export function formatEUR(decimalString: string): string {
  return formatter.format(Number(decimalString))
}

export function addDecimalStrings(a: string, b: string): string {
  return (Number(a) + Number(b)).toFixed(2)
}

export function multiplyDecimalString(decimalString: string, quantity: number): string {
  return (Number(decimalString) * quantity).toFixed(2)
}

// Integer minor units (cents) of a decimal string, as payment-service expects
// (PaymentDetails.amount is an Int). Rounded, since the decimal string is at most 2 digits.
export function toCents(decimalString: string): number {
  return Math.round(Number(decimalString) * 100)
}
