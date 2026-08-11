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
