const vacio = { data: undefined, isPending: false, isError: false, error: null }

export function useAnimal() {
  return { animal: vacio, treatments: vacio, eligibility: vacio }
}